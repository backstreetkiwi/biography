import { Component, OnInit } from '@angular/core';
import { TimelineService } from '../timeline.service';
import { TimelineDay } from '../types/timeline-day';

@Component( {
    selector: 'app-day-selector',
    templateUrl: './day-selector.component.html',
    styleUrls: ['./day-selector.component.css']
} )
export class DaySelectorComponent implements OnInit {

    timelineDays: TimelineDay[];

    day: number;

    constructor( private timelineService: TimelineService ) { }

    ngOnInit() {
        this.timelineService.monthChanged.subscribe( selectedMonth => {
            this.timelineDays = this.timelineService.getDays();
            this.day = null;
            this.timelineService.selectDay( null );
        } );
    }

    selectDay( day: number ) {
        this.timelineService.selectDay( day );
        this.day = day;
    }

    isSelected( day: number ): boolean {
        return this.day == day;
    }

}




