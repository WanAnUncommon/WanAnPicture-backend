package com.example.api.imagesearch.sub;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.example.exception.BusinessException;
import com.example.exception.ErrorCode;
import com.example.exception.ThrowUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 获取以图搜图页面地址
 *
 * @author WanAn
 */
@Slf4j
public class GetImagePageUrlApi {
    /**
     * 获取以图搜图页面地址
     *
     * @param imageUrl 图片地址
     * @return 以图搜图页面地址
     */
    public static String getImagePageUrl(String imageUrl) {
        // 准备请求参数
        Map<String, Object> formData = new HashMap<>();
        formData.put("image", imageUrl);
        formData.put("tn", "pc");
        formData.put("from", "pc");
        formData.put("image_source", "PC_UPLOAD_URL");
        // 构建请求地址
        long uptime = System.currentTimeMillis();
        String url = "https://graph.baidu.com/upload?uptime=" + uptime;
        String header = "jmM4zyI8OUixvSuWh0sCy4xWbsttVMZb9qcRTmn6SuNWg0vCO7N0s6Lffec+IY5yuqHujHmCctF9BVCGYGH0H5SH/H3VPFUl4O4CP1jp8GoAzuslb8kkQQ4a21Tebge8yhviopaiK66K6hNKGPlWt78xyyJxTteFdXYLvoO6raqhz2yNv50vk4/41peIwba4lc0hzoxdHxo3OBerHP2rfHwLWdpjcI9xeu2nJlGPgKB42rYYVW50+AJ3tQEBEROlg/UNLNxY+6200B/s6Ryz+n7xUptHFHi4d8Vp8q7mJ26yms+44i8tyiFluaZAr66/+wW/KMzOhqhXCNgckoGPX1SSYwueWZtllIchRdsvCZQ8tFJymKDjCf3yI/Lw1oig9OKZCAEtiLTeKE9/CY+Crp8DHa8Tpvlk2/i825E3LuTF8EQfzjcGpVnR00Lb4/8A";
        // 发送请求
        try (HttpResponse httpResponse = HttpUtil.createPost(url)
                .form(formData).header("Ask-Token", header).timeout(5000).execute();) {
            ThrowUtils.throwIf(!httpResponse.isOk(), ErrorCode.SYSTEM_ERROR, "请求调用接口失败");
            // 解析响应
            String body = httpResponse.body();
            Map<String, Object> result = JSONUtil.toBean(body, Map.class);
            // 处理响应结果
            if (result == null || !result.get("status").equals(0)) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "请求调用接口失败");
            }
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            // 解码url
            String rawUrl = (String) data.get("url");
            String searchResultUrl = URLUtil.decode(rawUrl);
            ThrowUtils.throwIf(CharSequenceUtil.isBlank(searchResultUrl), ErrorCode.SYSTEM_ERROR, "请求调用接口失败");
            return searchResultUrl;
        } catch (Exception e) {
            log.error("请求调用接口失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "请求调用接口失败");
        }
    }

    // 测试
    public static void main(String[] args) {
        String imageUrl = "https://tse1-mm.cn.bing.net/th/id/OIP-C.cHsa_yLVowCi422N2k0pJQHaFj?w=267&h=200&c=7&r=0&o=5&dpr=1.3&pid=1.7";
        String imagePageUrl = getImagePageUrl(imageUrl);
        System.out.println(imagePageUrl);
    }
}
