package word2vec;

import java.util.Collection;
import java.util.TreeSet;


//构建Haffman编码树
public class Haffman {
  private int layerSize;
  //private int topicNum;

  public Haffman(int layerSize) {
	  //this.topicNum = k;
	  this.layerSize = layerSize;
  }

  private TreeSet<Node> set = new TreeSet<>();

  public void make(Collection<Node> neurons) {
    set.addAll(neurons);
    while (set.size() > 1) {
      merger();
    }
  }

  private void merger() {
    HiddenNode hn = new HiddenNode(layerSize);
    Node min1 = set.pollFirst();
    Node min2 = set.pollFirst();
    hn.category = min2.category;
    hn.freq = min1.freq + min2.freq;
    min1.parent = hn;
    min2.parent = hn;
    min1.code = 0;
    min2.code = 1;
    set.add(hn);
  }

}
