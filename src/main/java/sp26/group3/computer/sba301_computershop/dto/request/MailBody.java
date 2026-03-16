package sp26.group3.computer.sba301_computershop.dto.request;


import lombok.Builder;

@Builder
public record MailBody(String to, String subject, String text) {

}