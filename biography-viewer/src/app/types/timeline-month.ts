export class TimelineMonth {
    
    year: number;
    month: number;
    count: number;
    monthName: string;

    private monthNames: string[] = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];

    constructor(year: number, month: number, count: number) {
        this.year = year;
        this.month = month;
        this.count = count;
        this.monthName = this.monthNames[this.month-1];
    }
}