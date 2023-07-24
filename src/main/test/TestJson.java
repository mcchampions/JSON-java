import org.json.JSONArray;
import org.json.JSONObject;
import org.json.TomlSerializer;
import org.junit.Test;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;

public class TestJson {
    @Test
    public void test() {
        JSONObject jsonObject = new JSONObject("{\"1\": \"3\"}");
        jsonObject.put("a","n");
        jsonObject.put("c","d");
        jsonObject.put("d",new JSONObject());
        jsonObject.put("e",new JSONArray());
        jsonObject.put("f",new JSONArray().put("1"));
        jsonObject.put("f",new JSONArray().put(new JSONObject()).put(new JSONObject().put("1","1")));
        jsonObject.put("gf",new JSONObject().put("1","1"));
        System.out.println(jsonObject.getJSONObject("2"));
        System.out.println(jsonObject.getInt("2"));
        System.out.println(jsonObject.getString("2"));
        System.out.println(jsonObject.getJSONArray("2"));
        System.out.println(jsonObject.get("2"));
        System.out.println(jsonObject.getDouble("2"));
        System.out.println(TomlSerializer.toToml(jsonObject));
        TomlParseResult t = Toml.parse("\"1\" = \"3\"\n" +
                "\"a\" = \"n\"\n" +
                "\"c\" = \"d\"\n" +
                "\"d\" = {}\n" +
                "\"e\" = []\n" +
                "\"f\" = [{},{\"1\" = \"1\"}]\n" +
                "\"gf\" = {\"1\" = \"1\"}");
        System.out.println(t);
        System.out.println(t.toToml());
        System.out.println(t.toMap());
        System.out.println(t.toJson());
    }
}
