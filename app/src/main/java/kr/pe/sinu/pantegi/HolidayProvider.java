package kr.pe.sinu.pantegi;

public interface HolidayProvider {
    public static final int NORMAL_DAY = 0;
    /**
     * This date must be shown in red color.
     */
    public static final int HOLIDAY = 1;
    /**
     * This date must be underlined.
     */
    public static final int SPECIAL_DAY = 2;

    /**
     * Called once a day (at midnight, or start of app) on separate thread, so networking can be done directly.
     * To prevent race condition, do NOT do any background work from here. Do everything synchronously.
     * It's probably best to do nothing in this function.
     */
    public void update();

    /**
     * Returns the holiday type of given day.
     * @param year it's a year
     * @param month Ranges from 1 to 12, first month is 1.
     * @param day Ranges from 1 to 31, first day is 1.
     * @return Bitflags of {@code NORMAL_DAY}, {@code HOLIDAY}, or {@code SPECIAL_DAY}.
     */
    public int getHoliday(int year, int month, int day);
}
