package project.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;

public class DateUtil {

    public static int getWorkingDaysInMonth(int month, int year) {
        YearMonth ym = YearMonth.of(year, month);
        int days = 0;
        for (int d = 1; d <= ym.lengthOfMonth(); d++) {
            DayOfWeek dow = LocalDate.of(year, month, d).getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) days++;
        }
        return days;
    }

    public static LocalDate firstDayOfMonth(int month, int year) {
        return LocalDate.of(year, month, 1);
    }

    public static LocalDate lastDayOfMonth(int month, int year) {
        return YearMonth.of(year, month).atEndOfMonth();
    }
}
