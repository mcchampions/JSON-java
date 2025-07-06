package org.json;

import java.util.Collection;
import java.util.Map;

/*
Public Domain.
*/

/**
 * JSONWriter provides a quick and convenient way of producing JSON text.
 * The texts produced strictly conform to JSON syntax rules. No whitespace is
 * added, so the results are ready for transmission or storage. Each instance of
 * JSONWriter can produce one JSON text.
 * <p>
 * A JSONWriter instance provides a <code>value</code> method for appending
 * values to the
 * text, and a <code>key</code>
 * method for adding keys before values in objects. There are <code>array</code>
 * and <code>endArray</code> methods that make and bound array values, and
 * <code>object</code> and <code>endObject</code> methods which make and bound
 * object values. All of these methods return the JSONWriter instance,
 * permitting a cascade style. For example, <pre>
 * new JSONWriter(myWriter)
 *     .object()
 *         .key("JSON")
 *         .value("Hello, World!")
 *     .endObject();</pre> which writes <pre>
 * {"JSON":"Hello, World!"}</pre>
 * <p>
 * The first method called must be <code>array</code> or <code>object</code>.
 * There are no methods for adding commas or colons. JSONWriter adds them for
 * you. Objects and arrays can be nested up to 200 levels deep.
 * <p>
 * This can sometimes be easier than using a JSONObject to build a string.
 * @author JSON.org
 * @version 2016-08-08
 */
public class JSONWriter {
    /**
     * Make a JSON text of an Object value. If the object has an
     * value.toJSONString() method, then that method will be used to produce the
     * JSON text. The method is required to produce a strictly conforming text.
     * If the object does not contain a toJSONString method (which is the most
     * common case), then a text will be produced by other means. If the value
     * is an array or Collection, then a JSONArray will be made from it and its
     * toJSONString method will be called. If the value is a MAP, then a
     * JSONObject will be made from it and its toJSONString method will be
     * called. Otherwise, the value's toString method will be called, and the
     * result will be quoted.
     *
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @param value
     *            The value to be serialized.
     * @return a printable, displayable, transmittable representation of the
     *         object, beginning with <code>{</code>&nbsp;<small>(left
     *         brace)</small> and ending with <code>}</code>&nbsp;<small>(right
     *         brace)</small>.
     * @throws JSONException
     *             If the value is or contains an invalid number.
     */
    public static String valueToString(Object value) throws JSONException {
        if (value == null) {
            return "null";
        }
        if (value instanceof JSONString) {
            String object;
            try {
                object = ((JSONString) value).toJSONString();
            } catch (Exception e) {
                throw new JSONException(e);
            }
            if (object != null) {
                return object;
            }
            throw new JSONException("error value");
        }
        if (value instanceof Number) {
            // not all Numbers may match actual JSON Numbers. i.e. Fractions or Complex
            final String numberAsString = JSONObject.numberToString((Number) value);
            if(JSONObject.NUMBER_PATTERN.matcher(numberAsString).matches()) {
                // Close enough to a JSON number that we will return it unquoted
                return numberAsString;
            }
            // The Number value is not a valid JSON number.
            // Instead we will quote it as a string
            return JSONObject.quote(numberAsString);
        }
        if (value instanceof Boolean || value instanceof JSONObject
                || value instanceof JSONArray) {
            return value.toString();
        }
        if (value instanceof Map<?, ?> map) {
            return new JSONObject(map).toString();
        }
        if (value instanceof Collection<?> coll) {
            return new JSONArray(coll).toString();
        }
        if (value.getClass().isArray()) {
            return new JSONArray(value).toString();
        }
        if(value instanceof Enum<?>){
            return JSONObject.quote(((Enum<?>)value).name());
        }
        return JSONObject.quote(value.toString());
    }
}
