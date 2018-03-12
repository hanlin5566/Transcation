package com.wiitrans.base.order;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import com.wiitrans.base.db.DictFactorDAO;
import com.wiitrans.base.db.model.DictFactorBean;
import com.wiitrans.base.log.Log4j;

public class OrderExpectedTime {

	private static int _start = 0;
	private static int _end = 0;

	// public static void main(String[] args) {
	// System.out.println(new Date().getTime());
	// System.out.println(new Date().getTime());
	// }

	private void GetFactor() {
		if (_start == 0 || _end == 0) {
			DictFactorDAO factordao = null;

			try {
				factordao = new DictFactorDAO();
				factordao.Init(true);
				DictFactorBean factor = factordao.Select();
				factordao.UnInit();
				// SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
				Calendar startDate = Calendar.getInstance();
				startDate.setTime(factor.start_time);
				Calendar endDate = Calendar.getInstance();
				endDate.setTime(factor.end_time);
				if (startDate.before(endDate)) {
					_start = startDate.get(Calendar.HOUR_OF_DAY) * 60
							+ startDate.get(Calendar.MINUTE);

					_end = endDate.get(Calendar.HOUR_OF_DAY) * 60
							+ endDate.get(Calendar.MINUTE);
					// end-start;
				} else {

				}
			} catch (Exception e) {
				// e.printStackTrace();
				Log4j.error(e);
			} finally {
				if (factordao != null) {
					factordao.UnInit();
				}
			}
		}
	}

	public int GetGetCutoffTimeForInt(Calendar now, int needtime) {
		return (int) (this.GetCutoffTime(now, needtime).getTime().getTime() / 1000L);
	}

	public String GetCutoffTimeForString(Calendar now, int needtime) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		return sdf.format(this.GetCutoffTime(now, needtime).getTime());
	}

	private Calendar GetCutoffTime(Calendar para, int needtime) {
		Calendar date = (Calendar) para.clone();
		if (needtime <= 0) {
			return date;
		}

		if (_start == 0 || _end == 0) {
			GetFactor();
		}

		int adddays = needtime / (_end - _start);
		int addminutes = needtime % (_end - _start);
		try {
			int hour = date.get(Calendar.HOUR_OF_DAY);
			int minute = date.get(Calendar.MINUTE);
			int begintime = hour * 60 + minute;

			if (begintime > _end) {
				begintime = _start;
				date.set(Calendar.HOUR_OF_DAY, 0);
				date.set(Calendar.MINUTE, 0);
				date.set(Calendar.SECOND, 0);
				date.set(Calendar.MILLISECOND, 0);
				date.add(Calendar.MINUTE, _start);
				date.add(Calendar.DATE, 1);
			} else if (begintime < _start) {
				begintime = _start;
				date.set(Calendar.HOUR_OF_DAY, 0);
				date.set(Calendar.MINUTE, 0);
				date.set(Calendar.SECOND, 0);
				date.set(Calendar.MILLISECOND, 0);
				date.add(Calendar.MINUTE, _start);
			}

			if (begintime + addminutes > _end) {
				addminutes = addminutes - (_end - _start);
				++adddays;
			}
			date.add(Calendar.DATE, adddays);
			date.add(Calendar.MINUTE, addminutes);

			return date;
		} catch (Exception e) {
			// e.printStackTrace();
			Log4j.error(e);
			return date;
		}
	}

	public int GetGetCutoffTimeForInt_new(Calendar now, int needtime) {
		return (int) (this.GetCutoffTime_new(now, needtime).getTime().getTime() / 1000L);
	}

	public String GetCutoffTimeForString_new(Calendar now, int needtime) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		return sdf.format(this.GetCutoffTime_new(now, needtime).getTime());
	}

	private Calendar GetCutoffTime_new(Calendar para, int needtime) {
		Calendar date = (Calendar) para.clone();
		if (needtime <= 0) {
			return date;
		}
		date.add(Calendar.MINUTE, needtime);

		return date;
	}
}
