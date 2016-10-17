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

        internal const String NEXT_CALL = "CALL::";
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

       

        public async static void http(SdSource source, bool isUpdate, HttpMessage msg) {
            log(source, "Util.http", msg.url);

            String cacheKey2 = null;
            String args = "";
            if (msg.form == null)
                cacheKey2 = msg.url;
            else {
                StringBuilder sb = new StringBuilder();
                sb.Append(msg.url);
                foreach (String key in msg.form.Keys) {
                    sb.Append(key).Append("=").Append(msg.form[key]).Append(";");
                }
                cacheKey2 = sb.ToString();
                args = cacheKey2;
            }
             String cacheKey = cacheKey2;

            __CacheBlock block = await cache.get(cacheKey);

            if (isUpdate == false && msg.config.cache > 0) {
                if (block != null && block.isOuttime(msg.config) == false) {
                    log(source, "Util.incache.url", msg.url);
                    msg.callback(1, msg, block.value, null);
                    return;
                }
            }

            doHttp(source, msg, block, (code, msg2, data, url302) => {
                if (code == 1) {
                    cache.save(cacheKey, data);
                }

                msg.callback(code, msg2, data, url302);
            });

            source.DoTraceUrl(msg.url, args, msg.config);
        }

        private static async void doHttp(SdSource source, HttpMessage msg, __CacheBlock cache, HttpCallback callback) {
            var encoding = Encoding.GetEncoding(msg.config.encode());

            AsyncHttpClient client = new AsyncHttpClient();
            client.UserAgent(msg.config.ua());
            client.Encoding(msg.config.encode());

            foreach (String key in msg.header.Keys) {
                client.Header(key, msg.header[key]);
            }

            string newUrl = null;
            if (msg.url.IndexOf(" ") >= 0)
                newUrl = Uri.EscapeUriString(msg.url);
            else
                newUrl = msg.url;

            client.Url(newUrl);

            string temp = null;

            AsyncHttpResponse rsp = null;
            try {
                if ("post".Equals(msg.config.method))
                    rsp = await client.Post(msg.form);
                else
                    rsp = await client.Get();

                if (rsp.StatusCode == HttpStatusCode.OK) {
                    source.setCookies(rsp.Cookies);
                    temp = rsp.GetString();

                    if (string.IsNullOrEmpty(rsp.location) == false) {
                        Uri uri = new  Uri(msg.url);
                        rsp.location = uri.Scheme + "://" + uri.Host + rsp.location;
                    }
                }
            }
            catch (Exception ex) {
                Util.log(source, "HTTP", ex.Message);
            }

            if (temp == null) {
                if (cache == null || cache.value == null)
                    callback(-2, msg, null, rsp.location);
                else
                    callback(1, msg, cache.value, rsp.location);
            }
            else
                callback(1, msg, temp, rsp.location);
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

        public static void log(SdSource source, SdNode node, String url, String json, int tag)
        {
            log(source, node.name, "tag=" + tag);

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
