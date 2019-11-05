<template>
    <div class="gallery">
        <div class="gallery-item" v-for="mediaFile in mediaFiles" v-bind:key="mediaFile.fileName">
            <div class="gallery-thumbnail">
                <img v-bind:src="mediaFile.thumbnailUrl"/>
                <div class="description-overlay"><div class="description">{{mediaFile.description}}</div></div>
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
div.gallery-item {
    position: relative;
    background-color: black;
    float:left;
    margin: 5px 5px 0px 0px;
    padding: 0px;
    height: 202px;
}

div.gallery-thumbnail {
    position: relative;
    margin: 1px;
    padding: 0px;
    height: 200px;
}

div.description-overlay {
    position: absolute;
    bottom: 0px;
    left: 0px;
    width: 100%;
    height: auto;
    overflow: hidden;
    background-color: rgba(0,0,0,0.5);
    z-index: 2;
    cursor: pointer;    
    padding: 0px;
}

div.description {
    color: white;
    font-size: 15px;
    padding: 5px;
    text-align: center;
}

</style>