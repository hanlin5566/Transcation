package com.wiitrans.base.file.ttx.parse;

import java.util.ArrayList;

import com.wiitrans.base.file.frag.Fragmentation.FRAG_TYPE;
import com.wiitrans.base.file.frag.TTXFragmentation;
import com.wiitrans.base.file.notag.BiliFileNoTag;
import com.wiitrans.base.file.notag.BiliFileNoTag.Content;
import com.wiitrans.base.file.sdlxliff.SDLXliffFileNoTag;
import com.wiitrans.base.file.sentence.TTXSentence;
import com.wiitrans.base.file.ttx.TTXFileNoTag;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.xml.XNode;

public class TTXParseDefaultImpl implements TTXParse {

	@Override
	public int ParseBody(XNode node, String source, String target,
			int lineindex,int percent, BiliFileNoTag fileNoTag) {
		int ret = Const.FAIL;

		int entityFragIndex = lineindex;

		XNode fragNode = new XNode("Frag");
		TTXFragmentation frag = new TTXFragmentation();

		int sentenceIndex = 0;

		ArrayList<Content> sents = new ArrayList<Content>();

		Content content = fileNoTag.new Content();

		content._tagcount = 0;

		content._valid = true;
		content._content = source;
		content._count = (short) (fileNoTag._sourceLang.AnalyseWord(Util
				.clearTag(source)).size());

		content._hashcode = Util.GetHashCode(source);

		sents.add(content);

		if ((sents != null) && (!sents.isEmpty())) {
			int fragcount = 0;
			int totalFragcount = 0;
			for (Content sent : sents) {
				XNode sentNode = new XNode("Sentence");
				TTXSentence ts = new TTXSentence();
				// ts._state = fileNoTag._state;
				ts._entityFragIndex = entityFragIndex;
				ts._entitySentenceIndex = sentenceIndex++;
				ts._source = sent._content;
				ts._totalSourceWordCount = sent._count;
				ts._sourceWordCount = (short) Math.round((percent>=80?ts._totalSourceWordCount*0.5:ts._totalSourceWordCount));
				// ts._translate = target;
				ts._recomTrans = target;
				ts._percent = percent;
				// ts._sourceTagCount = sent._tagcount;
				ts._hashcode = sent._hashcode;
				ts._valid = sent._valid;
				fragcount += ts._sourceWordCount;
				totalFragcount += ts._totalSourceWordCount;
				ts.SetNode(sentNode);
				fragNode.AddChild(sentNode);
				frag._sentences.add(ts);
			}
			frag._wordCount = fragcount;
			frag._totalwordCount = totalFragcount;
			frag._fragIndex = entityFragIndex;
			frag._sentenceCount = frag._sentences.size();
			frag._fragType = FRAG_TYPE.NONE;
			frag.SetNode(fragNode);
			node.AddChild(fragNode);
			fileNoTag._entityFrags.add(frag);

			fileNoTag._filesentencecount += frag._sentences.size();
			fileNoTag._filewordcount += fragcount;
			if(fileNoTag instanceof TTXFileNoTag){
			    ((TTXFileNoTag)fileNoTag)._totalfilewordcount += totalFragcount;
			}

		} else {
			// Log4j.error("Sentence is null or empty.");
		}

		fileNoTag._entityFragCount = fileNoTag._entityFrags.size();

		ret = Const.SUCCESS;

		return ret;
	}

}
