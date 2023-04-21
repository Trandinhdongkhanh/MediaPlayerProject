package Interface;

import java.util.ArrayList;

import Model.Song;

public interface IClickItem {
    void onClickItemSong(ArrayList<Song> songList, int pos);
}
