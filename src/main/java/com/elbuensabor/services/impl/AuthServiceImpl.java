package com.elbuensabor.services.impl;

import com.elbuensabor.dto.request.ClienteRegisterDTO;
import com.elbuensabor.dto.request.LoginRequestDTO;
import com.elbuensabor.dto.request.PasswordResetRequest;
import com.elbuensabor.dto.response.LoginResponseDTO;
import com.elbuensabor.entities.*;
import com.elbuensabor.exceptions.DuplicateResourceException;
import com.elbuensabor.exceptions.ResourceNotFoundException;
import com.elbuensabor.repository.IUsuarioRepository;
import com.elbuensabor.services.IAuthService;
import com.elbuensabor.services.IEmailService;
import com.elbuensabor.services.IImagenService;
import com.elbuensabor.services.mapper.ClienteMapper;
import com.elbuensabor.services.mapper.DomicilioMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements IAuthService {
    private final IUsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final ClienteMapper clienteMapper;
    private final DomicilioMapper domicilioMapper;
    private final IImagenService imagenService;
    private final JwtService jwtService;
    private final IEmailService emailService;

    public AuthServiceImpl(IUsuarioRepository usuarioRepository,
                           PasswordEncoder passwordEncoder,
                           ClienteMapper clienteMapper,
                           DomicilioMapper domicilioMapper,
                           IImagenService imagenService,
                           JwtService jwtService, IEmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.clienteMapper = clienteMapper;
        this.domicilioMapper = domicilioMapper;
        this.imagenService = imagenService;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public Usuario register(ClienteRegisterDTO request) {
        // 1. Verificar si el usuario ya existe
        if (usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateResourceException("El email ya está registrado.");
        }

        // 2. Crear la entidad Usuario
        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setRol(Rol.CLIENTE);
        usuario.setActivo(true);

        // 3. Crear la entidad Cliente (usando el mapper para los datos del perfil)
        Cliente cliente = new Cliente();
        cliente.setTelefono(request.getTelefono());
        cliente.setFechaNacimiento(request.getFechaNacimiento());

        // Establecer la relación bidireccional
        cliente.setUsuario(usuario);
        usuario.setCliente(cliente);

        // 4. Procesar Imagen (si existe)
        if (request.getImagen() != null && request.getImagen().getUrl() != null) {
            String url = request.getImagen().getUrl();
            String denominacion = "Perfil de " + request.getNombre();

            Imagen imagen = imagenService.createFromExistingUrl(denominacion, url);
            cliente.setImagen(imagen);
        }

        // 5. Procesar Domicilio
        Domicilio domicilio = domicilioMapper.toEntity(request.getDomicilio());
        domicilio.setCliente(cliente);
        cliente.getDomicilios().add(domicilio);

        // 6. Guardar el usuario (que cascadea el guardado del cliente y domicilio)
        return usuarioRepository.save(usuario);
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO request) {
        Usuario usuario = findByEmail(request.getEmail());

        if (passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            // Generar el token JWT
            String jwt = jwtService.generateToken(usuario);

            return new LoginResponseDTO(
                    jwt,
                    usuario.getEmail(),
                    usuario.getRol().name()
            );
        } else {
            // Manejo de contraseña inválida.
            throw new ResourceNotFoundException("Credenciales inválidas.");
        }
    }

    @Override
    public void requestPasswordReset(Usuario usuarioAutenticado) {
        // 1. Generar un token único y temporal (e.g., UUID o JWT de corto plazo)
        String resetToken = jwtService.generateResetToken(usuarioAutenticado);

        // 2. Almacenar el token en la base de datos o en caché con una fecha de expiración
        //    (e.g., usuarioAutenticado.setResetToken(resetToken); usuarioRepository.save(usuarioAutenticado);)
        usuarioAutenticado.setResetToken(resetToken);
        usuarioAutenticado.setTokenExpirationTime(System.currentTimeMillis() + 3600000); // 1 hora de validez
        usuarioRepository.save(usuarioAutenticado);

        // 3. Enviar el email con el enlace de reseteo
        String resetUrl = "http://frontend-url/reset-password?token=" + resetToken;
        emailService.sendPasswordResetEmail(usuarioAutenticado.getEmail(), resetUrl);
    }

    @Override
    public void resetPassword(PasswordResetRequest request) {
        // 1. Validar el token y su expiración
        Usuario usuario = usuarioRepository.findByResetToken(request.getToken())
                .orElseThrow(() -> new ResourceNotFoundException("Token de reseteo inválido."));

        if (usuario.getTokenExpirationTime() < System.currentTimeMillis()) {
            throw new ResourceNotFoundException("El token de reseteo ha expirado.");
        }

        // 2. Encriptar y actualizar la nueva contraseña
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        usuario.setPassword(encodedPassword);

        // 3. Limpiar el token de reseteo para evitar su reutilización
        usuario.setResetToken(null);
        usuario.setTokenExpirationTime(null);
        usuarioRepository.save(usuario);
    }

    @Override
    public void deleteAccount(Usuario usuarioAutenticado) {
        // 1. Lógica de borrado suave (soft delete) o duro
        usuarioRepository.delete(usuarioAutenticado);
    }

    @Override
    public Usuario findByEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));
    }
}
