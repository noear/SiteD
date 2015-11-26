using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Runtime.InteropServices.WindowsRuntime;
using System.Text;
using System.Threading.Tasks;
using Windows.Security.Cryptography;
using Windows.Security.Cryptography.Core;
using Windows.Storage.Streams;

using Noear.UWP.Http;
using Windows.Web.Http;

namespace org.noear.sited
{
    internal class Util
    {
        internal static __ICache cache = null;
        internal static void tryInitCache() {
            if (cache == null) {
                cache = new __FileCache("sited");
            }
        }

        public const String defUA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.135 Safari/537.36 Edge/12.10240";

       

        public static String urlEncode(String str, SdNode config)
        {
            try
            {
                return Uri.EscapeUriString(str);
            }
            catch (Exception)
            {
                return "";
            }
        }

        public async static void http(SdSource source, bool isUpdate, String url, Dictionary<String, String> args, SdNode config, HttpCallback callback) {
            __CacheBlock block = null;
            if (isUpdate == false && config.cache > 0) {
                block = cache.get(url);
            }

            if (block != null) {
                if (config.cache == 1 || block.seconds() <= config.cache) {
                    await Task.Delay(100);
                    callback(1, block.value);
                    return;
                }
            }

            doHttp(source, url, args, config, block,(code, data) => {
                if (code == 1 && config.cache > 0) {
                    cache.save(url, data);
                }

                callback(code, data);
            });
        }

        private static async void doHttp(SdSource source, String url, Dictionary<String, String> args, SdNode config, __CacheBlock cache, HttpCallback callback) {


            var encoding = Encoding.GetEncoding(config.encode());

            AsyncHttpClient client = new AsyncHttpClient();

            if (config.isInCookie() && string.IsNullOrEmpty(source.cookies()) == false) {
                client.Cookies(source.cookies());
            }

            client.Header("User-Agent", source.ua());
            client.Encoding(config.encode());

            string newUrl = null;
            if (url.IndexOf(" ") >= 0)
                newUrl = Uri.EscapeUriString(url);
            else
                newUrl = url;

            if (config.isInReferer()) {
                client.Header("Referer", source.buildReferer(config, url));
            }

            if (string.IsNullOrEmpty(config.accept) == false) {
                client.Header("Accept", config.accept);
                client.Header("X-Requested-With", "XMLHttpRequest");
            }

            client.Url(newUrl);

            string temp = null;

            try {
                AsyncHttpResponse rsp = null;
                if ("post".Equals(config.method))
                    rsp = await client.Post(args);
                else
                    rsp = await client.Get();

                if (rsp.StatusCode == HttpStatusCode.Ok) {
                    source.setCookies(rsp.Cookies);
                    temp = rsp.GetString();
                }
            }
            catch(Exception ex) {
                Util.log(source, "HTTP", ex.Message);
            }

            if (temp == null) {
                if (cache == null)
                    callback(-2, null);
                else
                    callback(1, cache.value);
            }
            else
                callback(1, temp);
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

        private static void log(SdSource source, String tag, String msg)
        {
            Debug.WriteLine(msg, tag);

            if (SdSource.logListener != null)
            {
                SdSource.logListener(source, tag, msg, null);
            }
        }

        public static void log(SdSource source, String tag, String msg, Exception tr)
        {
            Debug.WriteLine(msg, tag);

            if (SdSource.logListener != null)
            {
                SdSource.logListener(source, tag, msg, tr);
            }
        }
    }
}
