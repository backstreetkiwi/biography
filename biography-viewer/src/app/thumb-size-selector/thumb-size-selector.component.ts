import { Component, OnInit } from '@angular/core';
import { TimelineService } from '../timeline.service';
import { TimelineMonth } from '../types/timeline-month';

@Component( {
    selector: 'app-thumb-size-selector',
    templateUrl: './thumb-size-selector.component.html',
    styleUrls: ['./thumb-size-selector.component.css']
} )
export class ThumbSizeSelectorComponent implements OnInit {

    thumbnailSizes: number[];

    size: number;

    constructor( private timelineService: TimelineService ) { }

    ngOnInit() {
        this.thumbnailSizes = [200,300];
        this.size = this.timelineService.getSelectedThumbnailSize();
    }

    selectSize( size: number ) {
        this.size = size;
        this.timelineService.selectThumbnailSize( size );
    }

    isSelected( size: number ): boolean {
        return this.size == size;
    }

}




