import Foundation
import forecast

extension Date {
    /// "22 Sep" (order follows the device locale).
    var dayOfMonthLabel: String { formatted(.dateTime.day().month(.abbreviated)) }
    /// "Wednesday, 23 September" (order follows the device locale).
    var fullDateLabel: String { formatted(.dateTime.weekday(.wide).day().month(.wide)) }
    /// "14:30", always 24-hour like the Android screen.
    var timeOfDayLabel: String { formatted(.dateTime.hour(.twoDigits(amPM: .omitted)).minute(.twoDigits)) }
}

extension LocalDate {
    /// The date at midnight in the device time zone, so `Calendar`-based formatting keeps the same calendar day.
    var foundationDate: Date {
        Calendar.current.date(from: DateComponents(
            year: Int(year),
            month: Int(month.ordinal) + 1,
            day: Int(day)
        )) ?? .distantPast
    }
}

extension LocalDateTime {
    var foundationDate: Date {
        Calendar.current.date(from: DateComponents(
            year: Int(year),
            month: Int(month.ordinal) + 1,
            day: Int(day),
            hour: Int(hour),
            minute: Int(minute)
        )) ?? .distantPast
    }
}
