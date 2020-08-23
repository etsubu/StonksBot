package Core.YahooAPI;

import Core.YahooAPI.DataStructures.AssetPriceIntraInfo;
import Core.YahooAPI.DataStructures.DataResponse;

import java.io.IOException;
import java.util.Optional;

public interface YahooConnector {
    Optional<StockName> findTicker(String keyword) throws IOException, InterruptedException;
    Optional<AssetPriceIntraInfo> queryCurrentIntraPriceInfo(String keyword) throws IOException, InterruptedException;
    Optional<DataResponse> queryData(String ticker, String... typeList) throws IOException, InterruptedException;
}
