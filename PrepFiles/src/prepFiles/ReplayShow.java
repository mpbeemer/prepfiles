package prepFiles;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("REPLAY_SHOW")
public class ReplayShow {
	@XStreamAsAttribute
	@XStreamAlias("EPISODE")
	public String episode;
	@XStreamAsAttribute
	@XStreamAlias("ID")
	public String id;
	@XStreamAsAttribute
	@XStreamAlias("MEDIA_TYPE")
	public String mediaType;
	@XStreamAsAttribute
	@XStreamAlias("TITLE")
	public String title;
	@XStreamAlias("EPISODE_SUMMARY")
	public String episodeSummary;
	@XStreamAlias("ACTORS")
	public String actors;
	@XStreamAlias("GUEST_STARS")
	public String guestStars;
	@XStreamAlias("PRODUCERS")
	public String producers;
	@XStreamAlias("DIRECTOR")
	public String director;
	@XStreamAlias("RECORDING_INFO")
	public RecordingInfo recordingInfo;
	@XStreamAlias("MULTI_PART")
	public MultiPart multiPart;
	@XStreamAlias("TV_RATINGS")
	public TvRatings tvRatings;
	@XStreamAlias("MOVIE_INFO")
	public MovieInfo movieInfo;
	@XStreamAlias("LOCAL_SHOW_INFO")
	public LocalShowInfo localShowInfo;

	public ReplayShow() {
		this.episode = "";
		this.id = "";
		this.mediaType = "";
		this.title = "";
		this.episodeSummary = "";
		this.actors = "";
		this.guestStars = "";
		this.producers = "";
		this.director = "";
		this.recordingInfo = new RecordingInfo();
		this.multiPart = new MultiPart();
		this.tvRatings = new TvRatings();
		this.movieInfo = new MovieInfo();
		this.localShowInfo = new LocalShowInfo();
	}

	@XStreamAlias("RECORDING_INFO")
	public class RecordingInfo {
		@XStreamAsAttribute
		@XStreamAlias("DURATION_IN_SECONDS")
		public String durationInSeconds;
		@XStreamAsAttribute
		@XStreamAlias("GOP_COUNT")
		public String gopCount;
		@XStreamAsAttribute
		@XStreamAlias("IS_GUARANTEED")
		public String isGuaranteed;
		@XStreamAsAttribute
		@XStreamAlias("IS_MANUALLY_RECORDED")
		public String isManuallyRecorded;
		@XStreamAsAttribute
		@XStreamAlias("IS_PPV")
		public String isPPV;
		@XStreamAsAttribute
		@XStreamAlias("IS_REPEAT")
		public String isRepeat;
		@XStreamAsAttribute
		@XStreamAlias("RECORDED_AT")
		public String recordedAt;
		@XStreamAsAttribute
		@XStreamAlias("RECORDED_ON")
		public String recordedOn;
		@XStreamAsAttribute
		@XStreamAlias("RECORD_QUALITY")
		public String recordQuality;
		@XStreamAsAttribute
		@XStreamAlias("TV_CHANNEL_NUM")
		public String tvChannelNum;
		@XStreamAsAttribute
		@XStreamAlias("TV_CHANNEL_STATION_ID")
		public String tvChannelStationId;
		@XStreamAsAttribute
		@XStreamAlias("TV_CHANNEL_STATION_NAME")
		public String tvChannelStationName;

		public RecordingInfo() {
			this.durationInSeconds = "";
			this.gopCount = "";
			this.isGuaranteed = "";
			this.isManuallyRecorded = "";
			this.isPPV = "";
			this.isRepeat = "";
			this.recordedAt = "";
			this.recordedOn = "";
			this.recordQuality = "";
			this.tvChannelNum = "";
			this.tvChannelStationId = "";
			this.tvChannelStationName = "";
		}
	}
	
	@XStreamAlias("MULTI_PART")
	public class MultiPart {
		@XStreamAsAttribute
		@XStreamAlias("IS_MULTI_PART")
		public String isMultiPart;

		public MultiPart() {
			this.isMultiPart = "";
		}
	}
	
	@XStreamAlias("TV_RATINGS")
	public class TvRatings {
		@XStreamAsAttribute
		@XStreamAlias("TV_RATING")
		public String tvRating;
		@XStreamAsAttribute
		@XStreamAlias("TV_SUB_RATING")
		public String tvSubRating;

		public TvRatings() {
			this.tvRating = "";
			this.tvSubRating = "";
		}
	}
	
	@XStreamAlias("MOVIE_INFO")
	public class MovieInfo {
		@XStreamAsAttribute
		@XStreamAlias("IS_MOVIE")
		public String isMovie;

		public MovieInfo() {
			this.isMovie = "";
		}
	}

	@XStreamAlias("LOCAL_SHOW_INFO")
	public static class LocalShowInfo {
		@XStreamAsAttribute
		@XStreamAlias("BASE_FILENAME")
		public String baseFilename;
		@XStreamAlias("IS_FILENAME_CUSTOM")
		@XStreamAsAttribute
		public String isFilenameCustom;
		@XStreamAlias("IS_PUBLIC")
		@XStreamAsAttribute
		public String isPublic;
		@XStreamAlias("IS_READ_ONLY")
		@XStreamAsAttribute
		public String isReadOnly;
		@XStreamAlias("COMMENT")
		public String comment;

		public LocalShowInfo() {
			this.baseFilename = "";
			this.isFilenameCustom = "";
			this.isPublic = "";
			this.isReadOnly = "";
			this.comment = "";
		}
	}
	
}