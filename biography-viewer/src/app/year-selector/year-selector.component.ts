import { Component, OnInit } from '@angular/core';
import { TimelineService } from '../timeline.service';
import { TimelineYear } from '../types/timeline-year'

@Component( {
    selector: 'app-year-selector',
    templateUrl: './year-selector.component.html',
    styleUrls: ['./year-selector.component.css']
} )
export class YearSelectorComponent implements OnInit {

    timelineYears: TimelineYear[];

    year: number;

    constructor( private timelineService: TimelineService ) { }

    ngOnInit() {
        this.timelineYears = this.timelineService.getYears();
    }
    
    selectYear(year: number) {
        this.timelineService.selectYear(year);
        this.year = year;
    }
    
    isSelected(year: number): boolean {
        return this.year == year;
    }

}
