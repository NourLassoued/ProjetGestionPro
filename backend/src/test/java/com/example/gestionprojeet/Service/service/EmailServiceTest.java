package com.example.gestionprojeet.Service.service;

import com.example.gestionprojeet.Service.EmailService;
import com.example.gestionprojeet.classes.MailBody;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class EmailServiceTest {

    @Test
    void testSendEmail() {
        JavaMailSender mailSender = mock(JavaMailSender.class);

        EmailService emailService = new EmailService(mailSender);

        MailBody mailBody = new MailBody(
                "test@example.com",
                "Sujet Test",
                "Contenu Test"
        );

        emailService.setJavaMailSender(mailBody);

        ArgumentCaptor<SimpleMailMessage> messageCaptor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(mailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();

        assertEquals("test@example.com", sentMessage.getTo()[0]);
        assertEquals("Sujet Test", sentMessage.getSubject());
        assertEquals("Contenu Test", sentMessage.getText());
        assertEquals("nourlass50@gmail.com", sentMessage.getFrom());
    }
}