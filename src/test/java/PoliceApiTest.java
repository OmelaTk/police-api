import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import models.Region;
import org.testng.Assert;
import org.testng.annotations.Test;


import java.util.Map;

import static io.restassured.RestAssured.given;

public class PoliceApiTest {
    @Test
    public void checkFields() {
        String baseUrl = "https://data.police.uk/";
        String endPoint = "api/forces";
        ObjectMapper oMapper = new ObjectMapper();

        // Making call to get all regions
        Response response = given()
                .baseUri(baseUrl)
                .basePath(endPoint)
                .when()
                .get()
                .then()
                .statusCode(200)
                .extract()
                .response();
        JsonPath jsonPath = response.jsonPath();
        Region[] regions = jsonPath.getObject("$", Region[].class);

        // Iterate over returned regions and retrieving info on every location
        for (Region region : regions) {
            response = given()
                    .baseUri(baseUrl)
                    .basePath(endPoint + "/" + region.getId() + "/people")
                    .when()
                    .get()
                    .then()
                    .statusCode(200)
                    .extract()
                    .response();
            jsonPath = response.jsonPath();
            var location = jsonPath.getList("$");
            // Checking if location have officers
            if (location.size() > 0) {
                for (Object obj : location) {
                    Map<String, String> map = oMapper.convertValue(obj, Map.class);

                    // Asserting keys
                    Assert.assertTrue(map.containsKey("bio"), "Key 'bio' not found in region "+region.getName());
                    Assert.assertTrue(map.containsKey("name"), "Key 'name' not found in region "+region.getName());
                    Assert.assertTrue(map.containsKey("rank"), "Key 'rank' not found in region "+region.getName());
                    Assert.assertTrue(map.containsKey("contact_details"), "Key 'contact_details' not found in region "+region.getName());
                }
            }
        }
    }
}
