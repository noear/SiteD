using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Text;
using System.Threading.Tasks;
using Windows.Security.Cryptography;
using Windows.Security.Cryptography.Core;
using Windows.Storage.Streams;

using Noear.UWP.Http;
using System.Net;

namespace org.noear.sited {
    internal class Util
    {
        internal static __ICache cache = null;
        internal static void tryInitCache() {
            if (cache == null) {
                cache = new __FileCache("sited");
            }
        }

        public const String defUA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.135 Safari/537.36 Edge/12.10240";

        public static int parseInt(String str) {
            if (string.IsNullOrEmpty(str))
                return 0;
            else
                return int.Parse(str);
        }

        //----------

        public static String urlEncode(String str, SdNode config)
        {
            try
            {
                if(config.encode().ToLower() == "utf-8")
                    return Uri.EscapeUriString(str);
                else
                    return __Escape.JsEscape(str);
            }
            catch (Exception)
            {
                return "";
            }
        }

       

        public async static void http(SdSource source, bool isUpdate, String url, Dictionary<String, String> args, int tag, SdNode config, HttpCallback callback) {
            __CacheBlock block = null;
            String cacheKey = null;
            if (args == null)
                cacheKey = url;
            else {
                StringBuilder sb = new StringBuilder();
                sb.Append(url);
                foreach (String key in args.Keys) {
                    sb.Append(key).Append("=").Append(args[key]).Append(";");
                }
                cacheKey = sb.ToString();
            }

            if (isUpdate == false && config.cache > 0) {
                block = await cache.get(cacheKey);
            }

            if (block != null) {
                if (config.cache == 1 || block.seconds() <= config.cache) {
                    await Task.Delay(100);
                    callback(1, tag, block.value);
                    return;
                }
            }

            doHttp(source, url, args, tag, config, block, async (code, tag2, data) =>
            {
                if (code == 1 && config.cache > 0) {
                    await cache.save(cacheKey, data);
                }

                callback(code, tag2, data);
            });
        }

        private static async void doHttp(SdSource source, String url, Dictionary<String, String> args, int tag, SdNode config, __CacheBlock cache, HttpCallback callback) {
            var encoding = Encoding.GetEncoding(config.encode());

            AsyncHttpClient client = new AsyncHttpClient();
            client.UserAgent(config.ua());
            client.Encoding(config.encode());

            if (config.isInCookie()) {
                String cookies = config.cookies(url);
                if (cookies != null) {
                    client.Cookies(cookies);
                }
            }
            

         

            if (config.isInReferer()) {
                client.Referer(source.buildReferer(config, url));
            }

            if (string.IsNullOrEmpty(config.header) == false) {
                foreach (String kv in config.header.Split(';')) {
                    String[] kv2 = kv.Split('=');
                    if (kv2.Length == 2) {
                        client.Header(kv2[0], kv2[1]);
                    }
                }
            }

            string newUrl = null;
            if (url.IndexOf(" ") >= 0)
                newUrl = Uri.EscapeUriString(url);
            else
                newUrl = url;
            {
                int idx = newUrl.IndexOf('#'); //去除hash，即#.*
                String url2 = null;
                if (idx > 0)
                    url2 = newUrl.Substring(0, idx);
                else
                    url2 = url;

                client.Url(url2);
            }

            string temp = null;

            try {
                AsyncHttpResponse rsp = null;
                if ("post".Equals(config.method))
                    rsp = await client.Post(args);
                else
                    rsp = await client.Get();

                if (rsp.StatusCode == HttpStatusCode.OK) {
                    source.setCookies(rsp.Cookies);
                    temp = rsp.GetString();
                }
            }
            catch(Exception ex) {
                Util.log(source, "HTTP", ex.Message);
            }

            if (temp == null) {
                if (cache == null)
                    callback(-2,tag, null);
                else
                    callback(1,tag, cache.value);
            }
            else
                callback(1,tag, temp);
        }
        
        public static String md5(String code)
        {
            var alg = HashAlgorithmProvider.OpenAlgorithm(HashAlgorithmNames.Md5);
            IBuffer buff = CryptographicBuffer.ConvertStringToBinary(code, BinaryStringEncoding.Utf8);
            var hashed = alg.HashData(buff);
            return CryptographicBuffer.EncodeToHexString(hashed);

        }

        //
        //--------------------------------
        //

        public static void log(SdSource source, SdNode node, String url, String json)
        {
            if (url == null)
                log(source, node.name, "url=null");
            else
                log(source, node.name, url);

            if (json == null)
                log(source, node.name, "json=null");
            else
                log(source, node.name, json);
        }

        public static void log(SdSource source, String tag, String msg)
        {
            Debug.WriteLine(msg, tag);

            if (SdApi._listener != null)
            {
                SdApi._listener.run(source, tag, msg, null);
            }
        }

        public static void log(SdSource source, String tag, String msg, Exception tr)
        {
            Debug.WriteLine(msg, tag);

            if (SdApi._listener != null)
            {
                SdApi._listener.run(source, tag, msg, tr);
            }
        }

        //-------------
        //
        public static SdNode createNode(SdSource source) {
            return SdApi._factory.createNode(source);
        }

        public static SdNodeSet createNodeSet(SdSource source) {
            return SdApi._factory.createNodeSet(source);
        }
    }
}
