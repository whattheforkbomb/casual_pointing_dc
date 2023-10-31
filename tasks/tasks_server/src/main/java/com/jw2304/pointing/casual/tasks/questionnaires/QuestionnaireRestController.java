package com.jw2304.pointing.casual.tasks.questionnaires;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/questionnaire")
public class QuestionnaireRestController {

    public static Logger LOG = LoggerFactory.getLogger(QuestionnaireRestController.class);

    @Value("data.filepath")
    String rootFilePath;

    @PostMapping("/save/{pid}")
    public void save(@PathVariable(name="pid") String participantId, @RequestBody String surveyJson) {
        LOG.info("Payload Received: %s".formatted(surveyJson));
        String sessionFileName = "%s/%s/surveyResults.json".formatted(rootFilePath, participantId);
        File sessionSequenceFile = new File("%s".formatted(sessionFileName));
        sessionSequenceFile.getParentFile().mkdirs();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(sessionSequenceFile, true))) {
            bw.write(surveyJson);
        } catch (IOException ioex) {
            LOG.error("Unable to write to file: '%s'".formatted(sessionSequenceFile.getPath()), ioex);
        }
    }
    
}
