using System;
using System.Collections.Generic;
using System.Xml.Linq;

namespace org.noear.sited {
    public class SdNodeSet : ISdNode{
        

        List<ISdNode> _items = new List<ISdNode>();
        public readonly SdSource source;

        //---------------

        public SdNodeSet(SdSource source) {
            this.source = source;
        }

        public virtual void OnDidInit() {

        }

        private int _dtype = 0;
        public int dtype() {
            if (_dtype > 0)
                return _dtype;
            else
                return 1;
        }//数据类型

        public int nodeType() { return 2; }
        public String nodeName() { return name; }
        public bool isEmpty() {
            return _items.Count == 0;
        }

        public String name;
        public readonly SdAttributeList attrs = new SdAttributeList();

        internal SdNodeSet buildForNode(XElement element) {
            if (element == null)
                return this;

            name = element.Name.LocalName;

            _items.Clear();
            attrs.clear();

            {
                foreach (var p in element.Attributes()) {
                    attrs.set(p.Name.LocalName, p.Value);
                }
            }

            _dtype = attrs.getInt("dtype");

            foreach (XElement e1 in element.Elements()) {
                if (e1.HasAttributes) {
                    SdNode temp = Util.createNode(source).buildForNode(e1);
                    this.add(temp);
                }
                else {
                    SdNodeSet temp = Util.createNodeSet(source).buildForNode(e1);
                    this.add(temp);
                }
            }

            OnDidInit();

            return this;
        }

        public IEnumerable<ISdNode> nodes() {
            return _items;
        }

        public ISdNode get(String name) {
            foreach (ISdNode n in _items) {
                if (name.Equals(n.nodeName()))
                    return n;
            }

            return Util.createNode(source).buildForNode(null);
        }

        public SdNode nodeMatch(String url) {
            foreach (ISdNode n in nodes()) {
                SdNode n1 = (SdNode)n;
                if (n1.isMatch(url)) {
                    return n1;
                }
            }

            return Util.createNode(source).buildForNode(null);
        }

        internal void add(ISdNode node) {
            _items.Add(node);
        }
    }
}
