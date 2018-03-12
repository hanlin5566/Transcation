package com.wiitrans.base.file.lang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.wiitrans.base.db.model.DictTermBean;
import com.wiitrans.base.db.model.TermCustoBean;

public class MakeWordTree {

	private Language _lang;
	public int last_update_time = 0;
	private HashMap<String, WordNode> _map;

	private MakeWordTree() {
	}

	public MakeWordTree(Language lang) {
		this._lang = lang;
		this._map = new HashMap<String, WordNode>();
	}

	public void MakeCusto(List<TermCustoBean> list) {
		Queue<Word> queue;

		for (TermCustoBean termCusto : list) {
			queue = new LinkedList<Word>();
			ArrayList<Word> wordList = _lang.AnalyseWord(termCusto.term
					.toLowerCase());
			if (wordList.size() > 0) {
				for (Word word : wordList) {
					queue.offer(word);
				}

				SetNode(_map, queue, termCusto.term_id, termCusto.meaning);
			}
		}
	}

	public void MakeCusto(TermCustoBean bean) {
		Queue<Word> queue = new LinkedList<Word>();
		ArrayList<Word> wordList = _lang.AnalyseWord(bean.term.toLowerCase());
		if (wordList.size() > 0) {
			for (Word word : wordList) {
				queue.offer(word);
			}

			SetNode(_map, queue, bean.term_id, bean.meaning);
		}
	}

	public void Make(List<DictTermBean> list) {
		Queue<Word> queue;

		for (DictTermBean term : list) {
			queue = new LinkedList<Word>();
			ArrayList<Word> wordList = _lang.AnalyseWord(term.term
					.toLowerCase());
			if (wordList.size() > 0) {
				for (Word word : wordList) {
					queue.offer(word);
				}

				SetNode(_map, queue, term.term_id, null);
			}
		}
	}

	public void Make(DictTermBean bean) {
		Queue<Word> queue = new LinkedList<Word>();
		ArrayList<Word> wordList = _lang.AnalyseWord(bean.term.toLowerCase());
		if (wordList.size() > 0) {
			for (Word word : wordList) {
				queue.offer(word);
			}

			SetNode(_map, queue, bean.term_id, null);
		}
	}

	// 贪婪的把文本（单句多句均可）分解成术语，boolean tagFilter标签过滤，ttx sdl传入true，其他false
	public ArrayList<TextTerm> AnalyseGreedyText(String text, boolean tagFilter) {
		ArrayList<TextTerm> termInText = new ArrayList<TextTerm>();
		if (_map != null && text != null) {
			// 分解成句子
			ArrayList<LangSentence> sentences = _lang.AnalyseSentence(text
					.toLowerCase());
			int indexInText = 0;

			for (LangSentence sentence : sentences) {
				if (sentence.valid) {
					// 贪婪的分解成术语
					ArrayList<TextTerm> termList = AnalyseGreedySentence(_map,
							sentence.text, tagFilter);
					for (TextTerm textTerm : termList) {
						// System.out.println(sentence.substring(textTerm.index,textTerm.index
						// + textTerm.term.length()));
						// 计算文本中的index，原来的index所句子中的
						textTerm.index = textTerm.index + indexInText;
						termInText.add(textTerm);
					}
					indexInText += sentence.text.length();
				}

				return termInText;
			}
		}

		return termInText;
	}

	private ArrayList<TextTerm> AnalyseGreedySentence(
			HashMap<String, WordNode> map, String sentence, boolean tagFilter) {
		ArrayList<TextTerm> list = new ArrayList<TextTerm>();
		TextTerm term = null;
		// 存储上一个有效term，当句子扫描完成或者遇到不匹配节点，则校验term是否为空，
		// 不为空即有有效的匹配，把有效匹配存储进list并跳到有效匹配的j之后的一个位置继续循环
		// 为空即没有有效陪陪，则直接跳到i的下一个位置继续循环
		int lastTermIndex = -1;

		ArrayList<Word> wordList = _lang.AnalyseWord(sentence.toLowerCase());

		// 存储临时的map，由于所用while递归，tempmap：当上一个节点对应上时，tempmap为上一个节点的子map，当上一个节点没有对应上时，tempmap为总map
		HashMap<String, WordNode> tempmap = map;
		WordNode node;
		Word word;
		// i为起始位置，j为终止位置
		int i = 0, j = 0;
		while (wordList.size() > i && wordList.size() > j) {
			// 得到第j位置的节点，即即将匹配的节点
			word = wordList.get(j);
			if (tempmap != null && tempmap.containsKey(word.word)
					&& (!tagFilter || tagFilter && word.isAbled)) {
				// 匹配成功，并且单词可用
				node = tempmap.get(word.word);
				tempmap = node.subNodes;
				if (node.alsoLeaf) {
					// 有效匹配，把这个匹配存储到term中
					term = new TextTerm();
					term.term_id = node.term_id;
					term.index = wordList.get(i).charindex;
					term.term = sentence.substring(wordList.get(i).charindex,
							word.charindex + word.word.length());
					term.meaning = node.meaning;
					lastTermIndex = j++;

				} else {
					// 非有效匹配节点
					++j;
				}

				if (j >= wordList.size()) {
					// j为最后一个节点，不用继续匹配，
					tempmap = map;
					if (term == null) {
						i = i + 1;
						j = i;
					} else {

						i = lastTermIndex + 1;
						j = i;

						if (!list.contains(term)) {
							term.count = 1;
							list.add(term);
						} else {
							term = list.get(list.indexOf(term));
							term.count += 1;
						}

						lastTermIndex = -1;
						term = null;

					}
				}
			} else {
				// j为不匹配节点，不用继续匹配，
				tempmap = map;
				if (term == null) {
					i = i + 1;
					j = i;
				} else {

					i = lastTermIndex + 1;
					j = i;
					if (!list.contains(term)) {
						term.count = 1;
						list.add(term);
					} else {
						term = list.get(list.indexOf(term));
						term.count += 1;
					}
					lastTermIndex = -1;
					term = null;

				}
			}
		}
		return list;
	}

	private void SetNode(HashMap<String, WordNode> map, Queue<Word> queue,
			int term_id, String meaning) {
		Word word = queue.poll();
		WordNode node;
		if (map.containsKey(word.word)) {
			node = map.get(word.word);
		} else {
			node = new WordNode(word.word);
			map.put(word.word, node);
		}

		if (queue.size() > 0) {
			if (node.subNodes == null) {
				node.subNodes = new HashMap<String, WordNode>();
			}
			SetNode(node.subNodes, queue, term_id, meaning);
		} else {
			node.alsoLeaf = true;
			node.term_id = term_id;
			node.meaning = meaning;
		}
	}

	public HashMap<String, WordNode> GetMap() {
		return _map;
	}

	public void PrintTermList(String text, ArrayList<TextTerm> termList) {
		if (termList != null) {
			for (TextTerm textTerm : termList) {
				System.out.println("term:"
						+ textTerm.term
						+ String.format(" text.substring(%d,%d):",
								textTerm.index,
								(textTerm.index + textTerm.term.length()))
						+ text.substring(textTerm.index, textTerm.index
								+ textTerm.term.length()));
			}
		}
	}

}
