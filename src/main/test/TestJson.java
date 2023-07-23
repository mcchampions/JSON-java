import org.json.JSONObject;
import org.junit.Test;

public class TestJson {
    @Test
    public void test() {
        JSONObject jsonObject = new JSONObject("{\"1\": \"3\"}");
        System.out.println(jsonObject.getJSONObject("2"));
        System.out.println(jsonObject.getInt("2"));
        System.out.println(jsonObject.getString("2"));
        System.out.println(jsonObject.getJSONArray("2"));
        System.out.println(jsonObject.get("2"));
        System.out.println(jsonObject.getDouble("2"));
    }
}
