<template>
    <div class="gallery">
        <div class="media-file-thumb" v-for="mediaFile in mediaFiles">
            <img v-bind:src="mediaFile.thumbnailUrl"/>
            <br/>
            {{ mediaFile.description }}
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
div.media-file-thumb {
    float: left;
    background-color:darkslategrey;
    width: 200px;
    height: 200px;
    overflow: hidden;
    padding: 5px;
    margin: 5px;
    color: white;
}

img {
    width: 150px;
    height: 150px;
}
</style>