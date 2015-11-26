using System;
using System.Collections.Generic;
using System.Xml.Linq;

namespace org.noear.sited {
    public class SdNodeSet : ISdNode{
        public int nodeType() { return 2; }

        Dictionary<String, ISdNode> _items;
        SdSource _source;

        internal SdNodeSet(SdSource source) {
            _items = new Dictionary<string, ISdNode>();
            _source = source;
        }

        internal SdNodeSet(SdSource source, XElement element) :this(source){
           
            loadByElement(element);
        }

        internal void loadByElement(XElement element) {
            if (element == null)
                return;

            _items.Clear();

            foreach (XElement e1 in element.Elements()) {
                if (e1.HasAttributes) {
                    SdNode temp = new SdNode(_source, e1);
                    this.add(e1.Name.LocalName, temp);
                }
                else {
                    SdNodeSet temp = new SdNodeSet(_source, e1);
                    this.add(e1.Name.LocalName, temp);
                }
            }
        }

        public IEnumerable<ISdNode> nodes() {
            return _items.Values;
        }

        public ISdNode get(String name) {
            if (_items.ContainsKey(name))
                return _items[name];
            else
                return new SdNode(_source, null);
        }

        internal void add(String name, ISdNode node) {
            _items.Add(name, node);
        }
    }
}
