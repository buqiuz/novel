package io.github.xxyopen.novel.payment.dto.resp;
import io.github.xxyopen.novel.payment.dao.entity.WalletLog;
import lombok.Data;

import java.util.List;

@Data
public class WalletLogRespDto {
    private Long total;
    private List<WalletLog> walletLogs;
}
