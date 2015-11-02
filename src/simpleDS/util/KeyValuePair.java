/* @description This class is used to instantiate objects as key-value pairs.
 * 
 * @history 2.Nov.2015 Beta version
 *              
 * @author <ahref="mailto:h.cuayahuitl@gmail.com">Heriberto Cuayahuitl</a>
 */


package simpleDS.util;


public class KeyValuePair {
	private String key;
	private String value;

	public KeyValuePair(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}
	
	public String get() {
		return key + ":" + value;
	}
}
