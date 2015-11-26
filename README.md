# SiteD
SiteD

插件引擎实例（具本要根据应用面来定制自己的引擎）
```java
public class Source extends SdSource {

    public final SdNode hots;
    public final SdNode updates;
    public final SdNode search;
    public final SdNode tags;
    public final SdNodeSet home;

    public final SdNode tag;
    public final SdNode book;
    public final SdNode section;

    public Source(Application app, String xml) throws Exception {
        super(app, xml);

        home = (SdNodeSet) main.get("home");

        hots = (SdNode) home.get("hots");
        updates = (SdNode) home.get("updates");
        tags = (SdNode) home.get("tags");
        search = (SdNode) main.get("search");

        book = (SdNode) main.get("book");
        section = (SdNode) main.get("section");

        SdNode temp = (SdNode) main.get("tag");
        if (temp.isEmpty()) //旧版本
            tag = tags;
        else
            tag = temp; //新版本增加的:tags负责获取tag列表；tag负责获取解析tag.url的数据
    }

    @Override
    public void setCookies(String cookies) {
        super.setCookies(cookies);

        SiteDbApi.setSourceCookies(this);
    }

    public static boolean isHots(SdNode node){
        return "hots".equals(node.name);
    }

    public static boolean isUpdates(SdNode node){
        return "updates".equals(node.name);
    }

    public static boolean isTags(SdNode node){
        return "tags".equals(node.name);
    }

    public static boolean isBook(SdNode node){
        return "book".equals(node.name);
    }

    public static boolean isSearch(SdNode node){
        return "search".equals(node.name);
    }

    public static boolean isSection(SdNode node){
        return "section".equals(node.name);
    }


    private boolean _isAlerted = false;
    public boolean tryAlert(ActivityBase activity,Act1<Boolean> callback) {
        if (TextUtils.isEmpty(alert))
            return false;
        else {
            if (_isAlerted == false) {
                new AlertDialog.Builder(activity)
                        .setTitle("提示")
                        .setMessage(alert)
                        .setNegativeButton("退出", (d, w) -> {
                            _isAlerted = false;
                            d.dismiss();
                            callback.run(false);
                        })
                        .setPositiveButton("继续", (d, w) -> {
                            _isAlerted = true;
                            d.dismiss();
                            callback.run(true);
                        }).setCancelable(false).show();
            }

            return true;
        }
    }
}
```
