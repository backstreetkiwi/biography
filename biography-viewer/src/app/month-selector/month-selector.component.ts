import { Component, OnInit } from '@angular/core';
import { TimelineService } from '../timeline.service';
import { TimelineMonth } from '../types/timeline-month';

@Component( {
    selector: 'app-month-selector',
    templateUrl: './month-selector.component.html',
    styleUrls: ['./month-selector.component.css']
} )
export class MonthSelectorComponent implements OnInit {

    timelineMonths: TimelineMonth[];

    month: number;

    constructor( private timelineService: TimelineService ) { }

    ngOnInit() {
        this.timelineService.yearChanged.subscribe(selectedYear => {
           this.timelineMonths = this.timelineService.getMonths();
           this.month = null;
           this.timelineService.selectMonth(null);
        });
    }

    selectMonth(month: number) {
        this.month = month;
        this.timelineService.selectMonth(month);
    }
    
    isSelected(month: number): boolean {
        return this.month == month;
    }
}








