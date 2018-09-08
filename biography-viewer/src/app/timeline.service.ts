import { Injectable } from '@angular/core';
import { Output } from '@angular/core';
import { EventEmitter } from '@angular/core';
import { TimelineYear } from './types/timeline-year';
import { TimelineMonth } from './types/timeline-month';
import { TimelineDay } from './types/timeline-day';
import { MediaFile } from './types/media-file';
import { HttpClient } from '@angular/common/http';

@Injectable( {
    providedIn: 'root'
} )
export class TimelineService {

    private selectedYear: number;
    private selectedMonth: number;
    private selectedDay: number;
    private selectedThumbnailSize: number;

    @Output() yearChanged: EventEmitter<number> = new EventEmitter();
    @Output() monthChanged: EventEmitter<number> = new EventEmitter();
    @Output() dayChanged: EventEmitter<number> = new EventEmitter();
    @Output() thumbnailSizeChanged: EventEmitter<number> = new EventEmitter();

    constructor( private http: HttpClient ) {
        this.selectedThumbnailSize = 200;
    }

    getSelectedYear(): number {
        return this.selectedYear;
    }

    getSelectedMonth(): number {
        return this.selectedMonth;
    }

    getSelectedDay(): number {
        return this.selectedDay;
    }

    getSelectedThumbnailSize(): number {
        return this.selectedThumbnailSize;
    }

    selectYear( year: number ): void {
        this.selectedYear = year;
        this.yearChanged.emit( this.selectedYear );
    }

    selectMonth( month: number ): void {
        this.selectedMonth = month;
        this.monthChanged.emit( this.selectedMonth );
    }

    selectDay( day: number ): void {
        this.selectedDay = day;
        this.dayChanged.emit( this.selectedDay );
    }

    selectThumbnailSize( thumbnailSize: number ): void {
        this.selectedThumbnailSize = thumbnailSize;
        this.thumbnailSizeChanged.emit( this.selectedThumbnailSize );
    }

    getYears(): TimelineYear[] {

        var years: TimelineYear[] = [];

        this.http.get( 'http://localhost:8080/rest/mediafiles/' ).subscribe( data => {
            for ( var idx in data ) {
                years.push( { year: data[idx]['year'], count: data[idx]['count'] } );
            }
        }
        );

        return years;
    }

    getMonths(): TimelineMonth[] {

        var months: TimelineMonth[] = [];

        if ( this.selectedYear == null ) {
            return months;
        }

        this.http.get( 'http://localhost:8080/rest/mediafiles/' + this.selectedYear + '/' ).subscribe( data => {
            for ( var idx in data ) {
                var yearMonth: string;
                yearMonth = data[idx]['yearMonth'];
                var month: number;
                month = parseInt( yearMonth.substring( 5 ) );
                months.push( new TimelineMonth( this.selectedYear, month, data[idx]['count'] ) );
            }
        }
        );

        return months;
    }

    getDays(): TimelineDay[] {

        var days: TimelineDay[] = [];
        if ( this.getSelectedMonth() == null ) {
            return days;
        }


        this.http.get( 'http://localhost:8080/rest/mediafiles/' + this.selectedYear + '/' + this.selectedMonth + '/' ).subscribe( data => {
            for ( var idx in data ) {
                var date: string;
                date = data[idx]['date'];
                var day: number;
                day = parseInt( date.substring( 8 ) );
                days.push( new TimelineDay( this.selectedYear, this.selectedMonth, day, data[idx]['count'] ) );
            }
        }
        );

        return days;
    }

    getMediaFiles(): MediaFile[] {

        var mediaFiles: MediaFile[] = [];

        if (this.selectedDay==null) {
            return mediaFiles;
        }
    
        this.http.get( 'http://localhost:8080/rest/mediafiles/' + this.selectedYear + '/' + this.selectedMonth + '/' + this.selectedDay + '/' ).subscribe( data => {
            for ( var idx in data['mediaFiles'] ) {
                mediaFiles.push( new MediaFile( data['mediaFiles'][idx]['fileName'], data['mediaFiles'][idx]['description'] ) );
            }
        }
        );

        return mediaFiles;
    }
}
