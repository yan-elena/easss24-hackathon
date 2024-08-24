package artifact;

import cartago.Artifact;
import cartago.OPERATION;
import cartago.OpFeedbackParam;

import java.util.Random;

public class NewsSource extends Artifact {

    private Random random = new Random();

    @OPERATION
    void getTopic(OpFeedbackParam<String> topic) {
        int id = random.nextInt(2);
        log("source topic: " + id);
        topic.set(String.valueOf(id)); //topic id
    }

}
