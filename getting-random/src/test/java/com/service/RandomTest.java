import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

import com.service.TestProfileForRandom;

@QuarkusTest
@TestProfile(TestProfileForRandom.class)
public class RandomTest {

    @Test
    public void testRandomEndpoint() {
        given()
          .when().get("/random")
          .then()
             .statusCode(200)
             .body(containsString("error"));
    }
}
