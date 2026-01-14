import org.junit.runner.RunWith;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "Features",
        plugin = {
                "pretty",
                "json:build/reports/cucumber/cucumber.json"
        }
)
public class CucumberTest {
}
