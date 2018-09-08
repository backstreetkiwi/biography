import { Component, OnInit } from '@angular/core';
import { MediaFile } from '../types/media-file';
import { TimelineService } from '../timeline.service';
import { FileviewerService } from '../fileviewer.service';

@Component( {
    selector: 'app-images',
    templateUrl: './images.component.html',
    styleUrls: ['./images.component.css']
} )
export class ImagesComponent implements OnInit {

    mediaFiles: MediaFile[];

    thumbnailSize: number;

    constructor( private timelineService: TimelineService, private fileviewerService: FileviewerService ) { }

    ngOnInit() {
        this.timelineService.dayChanged.subscribe( selectedDay => {
            this.mediaFiles = this.timelineService.getMediaFiles();
        } );
        this.timelineService.thumbnailSizeChanged.subscribe( selectedThumbnailSize => {
            this.thumbnailSize = selectedThumbnailSize;
        } );
        this.thumbnailSize = this.timelineService.getSelectedThumbnailSize();
    }
    
    show(mediaFile: MediaFile): void {
        this.fileviewerService.show(mediaFile);
    }

}
