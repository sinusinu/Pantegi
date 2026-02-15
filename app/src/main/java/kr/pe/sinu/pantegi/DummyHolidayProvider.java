package kr.pe.sinu.pantegi;

public class DummyHolidayProvider implements HolidayProvider {
    @Override
    public void update() {
        // do nothing
    }

    @Override
    public int getHoliday(int year, int month, int day) {
        if (year == 1970 && month == 1 && day == 1) return HOLIDAY;
        return NORMAL_DAY;
    }
}
