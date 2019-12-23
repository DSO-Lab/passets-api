import com.defvul.passets.api.util.DateUtil;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 说明:
 * 时间: 2019/12/23 15:28
 *
 * @author wimas
 */
public class TestEs {

    public RestHighLevelClient client() {
        return new RestHighLevelClient(
                RestClient.builder(HttpHost.create("http://10.87.222.222:9200"))
        );
    }

    @Test
    public void TestEs() throws IOException {
        GetAliasesResponse response = client().indices().getAlias(new GetAliasesRequest(), RequestOptions.DEFAULT);
        List<String> result = response.getAliases().keySet().stream().filter(r -> notExpire(r)).collect(Collectors.toList());
        System.out.println(result);

    }

    private boolean notExpire(String indexStr) {
        String i = "logstash-passets" + "-";
        if (indexStr.indexOf(i) != 0) {
            return false;
        }

        String[] is = indexStr.split(i);
        String min = DateUtil.format(DateUtil.add(new Date(), -1), DateUtil.YYYYMMDD);
        return Long.valueOf(is[1])< Long.valueOf(min);
    }
}
