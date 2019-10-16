package uk.gov.ons.collection.entity;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FormDefinitionEntity {
    private Integer formId;
    private String questionCode;
    private String displayQuestionNumber;
    private String displayText;
    private String type;
    private String createdBy;
    private String createdDate;
    private String lastUpdatedBy;
    private String lastUpdatedDate;
}
