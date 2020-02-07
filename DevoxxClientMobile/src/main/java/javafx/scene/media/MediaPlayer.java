package javafx.scene.media;

public class MediaPlayer {
    public enum Status {

        /**
         * State of the player immediately after creation. While in this state,
         * property values are not reliable and should not be considered.
         * Additionally, commands sent to the player while in this state will be
         * buffered until the media is fully loaded and ready to play.
         */
        UNKNOWN,
        /**
         * State of the player once it is prepared to play.
         * This state is entered only once when the movie is loaded and pre-rolled.
         */
        READY,
        /**
         * State of the player when playback is paused. Requesting the player
         * to play again will cause it to continue where it left off.
         */
        PAUSED,
        /**
         * State of the player when it is currently playing.
         */
        PLAYING,
        /**
         * State of the player when playback has stopped.  Requesting the player
         * to play again will cause it to start playback from the beginning.
         */
        STOPPED,
        /**
         * State of the player when data coming into the buffer has slowed or
         * stopped and the playback buffer does not have enough data to continue
         * playing. Playback will continue automatically when enough data are
         * buffered to resume playback. If paused or stopped in this state, then
         * buffering will continue but playback will not resume automatically
         * when sufficient data are buffered.
         */
        STALLED,
        /**
         * State of the player when a critical error has occurred.  This state
         * indicates playback can never continue again with this player.  The
         * player is no longer functional and a new player should be created.
         */
        HALTED,
        /**
         * State of the player after dispose() method is invoked. This state indicates
         * player is disposed, all resources are free and player SHOULD NOT be used again.
         * <code>Media</code> and <code>MediaView</code> objects associated with disposed player can be reused.
         * @since JavaFX 8.0
         */
        DISPOSED
    }
}
