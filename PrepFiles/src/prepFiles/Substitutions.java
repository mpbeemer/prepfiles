package prepFiles;

import java.util.ArrayList;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("substitutions")
public class Substitutions {
	@XStreamImplicit
	public ArrayList<Alias> alias_list = new ArrayList<Alias>();

	@XStreamAlias("substitution")
	public class Alias {
		// Provided suggested file name aliases
		@XStreamAlias("series")
		private String series_name;
		@XStreamAlias("alias")
		private String series_alias;
		
		public String getName() {
			return series_name;
		}
		public void setName(String series_name) {
			this.series_name = series_name;
		}
		public String getAlias() {
			return series_alias;
		}
		public void setAlias(String series_alias) {
			this.series_alias = series_alias;
		}
		
		Alias(String series_name, String series_alias) {
			this.series_name = series_name;
			this.series_alias = series_alias;
		}
	}

}
