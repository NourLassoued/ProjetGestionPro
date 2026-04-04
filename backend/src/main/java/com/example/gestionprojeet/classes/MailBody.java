package com.example.gestionprojeet.classes;

import lombok.Builder;

@Builder
public record MailBody(String to ,String subject,String text) {

}
