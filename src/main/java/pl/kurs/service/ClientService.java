package pl.kurs.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kurs.dto.ClientDto;
import pl.kurs.entity.Client;
import pl.kurs.exception.EmailNotVerifiedException;
import pl.kurs.exception.InvalidVerificationTokenException;
import pl.kurs.exception.ResourceNotFoundException;
import pl.kurs.mapper.ClientMapper;
import pl.kurs.repository.ClientRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClientService {
    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;
    private final MailService mailService;

    @Transactional
    public ClientDto createClient(ClientDto dto) {
        String verificationToken = UUID.randomUUID().toString();

        Client client = clientMapper.dtoToEntity(dto);
        client.setVerificationToken(verificationToken);

        Client savedClient = clientRepository.save(client);
        mailService.sendVerificationEmail(dto.getEmail(), verificationToken);

        return clientMapper.entityToDto(savedClient);
    }

    @Transactional
    public void verifyEmail(String token) {
        Client client = clientRepository.findByVerificationToken(token)
                .orElseThrow(() -> new InvalidVerificationTokenException("The token is invalid or has already been used"));
        client.setEmailVerified(true);
        client.setVerificationToken(null);
        clientRepository.save(client);
        mailService.sendEmailVerifiedConfirmation(client.getEmail());
    }

    public Client findClientById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));
    }

    public Client findVerifiedClientById(Long id) {
        Client client = findClientById(id);
        if (!client.getEmailVerified()) {
            throw new EmailNotVerifiedException("Email must be verified before creating subscription");
        }
        return client;
    }
}
