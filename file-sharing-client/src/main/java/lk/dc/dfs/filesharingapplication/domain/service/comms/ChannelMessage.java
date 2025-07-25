package lk.dc.dfs.filesharingapplication.domain.service.comms;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChannelMessage {
    private  String address;
    private  int port;
    private  String message;

}
