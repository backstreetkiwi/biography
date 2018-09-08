import { Component, OnInit } from '@angular/core';
import { FileviewerService } from '../fileviewer.service';
import { MediaFile } from '../types/media-file';

@Component( {
    selector: 'app-zoom-viewer',
    templateUrl: './zoom-viewer.component.html',
    styleUrls: ['./zoom-viewer.component.css']
} )
export class ZoomViewerComponent implements OnInit {

    visible: boolean;
    mediaFile: MediaFile;
    filename: string;

    constructor( private fileviewerService: FileviewerService ) { this.visible = false; }

    ngOnInit() {
        this.fileviewerService.fileSet.subscribe( (mediaFile: MediaFile) => {
            this.mediaFile = mediaFile;
            this.filename = mediaFile.fileName;
            this.visible = true;
        } );
    }

    hide() {
        this.visible = false;
    }

}
