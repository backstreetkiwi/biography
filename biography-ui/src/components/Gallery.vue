<template>
    <div class="gallery">
        <div class="galleryitem" v-for="mediaFile in mediaFiles" v-bind:key="mediaFile.fileName">
            <div class="media-file-thumb" v-bind:style="{ 'background-image': 'url(' + mediaFile.thumbnailUrl + ')', 'background-size': 'cover' }">
                <span>{{ mediaFile.description }}</span>
            </div>
        </div>
    </div>
</template>

<script>
    import axios from "axios";

    export default {
        name: 'Gallery',
        props: {
            mediaFiles: {
                type: Array,
                default: () => []
            }
        }
    }
</script>

<style scoped>
div.galleryitem {
    background-color:black;
    width: 210px;
    height: 210px;
    float:left;
    margin: 3px;
}

div.media-file-thumb {
    position: relative;
    height: 200px;
    width: 200px;
    margin: 5px;
    align-items: baseline;
}

span {
    background-color:rgba(0, 0, 0, 0.6);
    position: absolute;
    bottom: 0;
    left: 0;
    color:white;
    width: 100%;
}

</style>