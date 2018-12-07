package prepFiles;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("DVARCHIVE_SHOW")
public class DVArchiveShow {
	@XStreamAlias("REPLAY_CATEGORY")
	ReplayCategory replayCategory;

	@XStreamAlias("REPLAY_CATEGORY")
	public class ReplayCategory {
		@XStreamAsAttribute
		@XStreamAlias("ID")
		public String id;
		@XStreamAsAttribute
		@XStreamAlias("NAME")
		public String name;
		@XStreamAsAttribute
		@XStreamAlias("PUBLIC")
		public String isPublic;
		@XStreamAsAttribute
		@XStreamAlias("READ_ONLY")
		public String readOnly;
		@XStreamAlias("COMMENT")
		public String comment;
		@XStreamAlias("REPLAY_CHANNEL")
		public ReplayChannel replayChannel;

		public ReplayCategory() {
		}
	}

	@XStreamAlias("REPLAY_CHANNEL")
	public class ReplayChannel {
		@XStreamAsAttribute
		@XStreamAlias("ID")
		public String id;
		@XStreamAsAttribute
		@XStreamAlias("NAME")
		public String name;
		@XStreamAsAttribute
		@XStreamAlias("PUBLIC")
		public String isPublic;
		@XStreamAsAttribute
		@XStreamAlias("READ_ONLY")
		public String readOnly;
		@XStreamAlias("COMMENT")
		public String comment;
		@XStreamAlias("REPLAY_SHOW")
		public ReplayShow replayShow;

		public ReplayChannel() {
		}
	}
	
	public String toString() {
		return this.replayCategory.replayChannel.replayShow.episode + ": " + this.replayCategory.replayChannel.replayShow.id;
	}
}
