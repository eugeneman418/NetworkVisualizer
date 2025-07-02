const modes = ['truck', 'train', 'barge'];

const appState = {
    timeStep: 0,
    show_truck: true,
    show_train: true,
    show_barge: true,
    selected_node: null,
    selected_link: null
};

const map = L.map('map').setView([51.505, -0.09], 13);
L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '© OpenStreetMap'
}).addTo(map);

const slider = document.getElementById('slider');
const sliderValue = document.getElementById('sliderValue');
const modeCheckboxesContainer = document.getElementById('modeCheckboxes');
const colorBarRightLabel = document.querySelector('.color-label.right');

let metadata = null;
let selectedModes = new Set();

async function fetchMetadata() {
    try {
        const response = await fetch('http://localhost:8000/metadata');
        if (!response.ok) throw new Error('Failed to fetch metadata');
        metadata = await response.json();

        setupSlider(metadata.time_steps);
        setupModeCheckboxes();
        updateColorBarLabel();
        fetchGraph();
    } catch (err) {
        console.error('Error fetching metadata:', err);
    }
}

function setupSlider(timeSteps) {
    slider.min = 0;
    slider.max = timeSteps.length - 1;
    slider.step = 1;
    slider.value = 0;
    sliderValue.textContent = timeSteps[0];

    slider.addEventListener("input", () => {
        appState.timeStep = parseInt(slider.value);
        sliderValue.textContent = timeSteps[appState.timeStep];
        updateGraph();
    });
}

function setupModeCheckboxes() {
    modeCheckboxesContainer.innerHTML = '';

    modes.forEach(mode => {
        const stateKey = `show_${mode}`;
        appState[stateKey] = true;

        const label = document.createElement('label');
        const checkbox = document.createElement('input');
        checkbox.type = 'checkbox';
        checkbox.checked = true;
        checkbox.value = mode;

        checkbox.addEventListener('change', () => {
            appState[stateKey] = checkbox.checked;
            updateGraph();
        });

        label.appendChild(checkbox);
        label.appendChild(document.createTextNode(' ' + mode));
        modeCheckboxesContainer.appendChild(label);
    });
}

function getSelectedModes() {
    return modes.filter(mode => appState[`show_${mode}`]);
}

function updateColorBarLabel() {
    if (!metadata) return;
    let maxIntensity = 0;
    getSelectedModes().forEach(mode => {
        const val = metadata.max_intensities[mode];
        if (val !== undefined && val > maxIntensity) {
            maxIntensity = val;
        }
    });

    colorBarRightLabel.textContent = `Max Intensity: ${maxIntensity}`;
}

async function fetchGraph() {
    try {
        const response = await fetch('http://localhost:8000/graph');
        if (!response.ok) throw new Error('Failed to fetch graph');
        const graph = await response.json();
        displayGraph(graph);
        updateIntensity();
    } catch (err) {
        console.error('Error fetching graph:', err);
    }
}

async function updateIntensity() {
    if (!metadata) return;
    const timeIndex = parseInt(slider.value);
    const time = metadata.time_steps[timeIndex];

    const selectedModes = getSelectedModes();

    if (selectedModes.length === 0) {
        if (window.polylines) {
            window.polylines.forEach(polyline => polyline.setStyle({ opacity: 0 }));
        }
        updateTable([]);
        return;
    }

    const params = new URLSearchParams();
    params.set('time', timeIndex);
    params.set('modes', selectedModes.join(','));

    try {
        const response = await fetch(`http://localhost:8000/intensity?${params.toString()}`);
        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        const intensities = await response.json();

        if (!window.polylines || window.polylines.length !== intensities.length) return;

        let maxIntensity = Math.max(...Array.from(selectedModes).map(m => metadata.max_intensities[m] || 0));

        function intensityToColor(intensity, max) {
            const ratio = Math.min(intensity / max, 1);
            const r = Math.floor(255 * ratio);
            const g = Math.floor(255 * (1 - ratio));
            return `rgb(${r},${g},0)`;
        }

        window.polylines.forEach((polyline, i) => {
            const intensity = intensities[i];
            if (intensity === 0) {
                polyline.setStyle({ opacity: 0 });
            } else {
                polyline.setStyle({ color: intensityToColor(intensity, maxIntensity), opacity: 1 });
            }
        });

    } catch (error) {
        console.error('Failed to fetch intensity data:', error);
    }
}

function displayGraph(graph) {
    if (window.graphLayers) window.graphLayers.forEach(layer => map.removeLayer(layer));
    window.graphLayers = [];
    const bounds = L.latLngBounds();

    graph.nodes.forEach(node => {
        const latlng = [node.lat, node.lon];
        bounds.extend(latlng);
        const marker = L.marker(latlng).addTo(map);
        marker.on("click", () => {
            appState.selected_node = node.name;
            appState.selected_link = null;
            fetchNodeEvents();
        });
        window.graphLayers.push(marker);
    });

    window.polylines = [];

    graph.links.forEach((link, index) => {
        const polyline = L.polyline(link.path, {
            color: 'blue',
            weight: 3,
            opacity: 0.7
        }).addTo(map);

        polyline.on('click', () => {
            appState.selected_node = null;
            appState.selected_link = index;
            fetchLinkEvents();
        });


        window.polylines.push(polyline);
    });

    if (bounds.isValid()) {
        map.fitBounds(bounds, { padding: [20, 20] });
    }
}

async function fetchLinkEvents() {
    const selectedModes = getSelectedModes();
    if (!selectedModes.length) return;

    
    const params = new URLSearchParams({ path: appState.selected_link, time: metadata.time_steps[appState.timeStep], modes: selectedModes.join(',') });

    try {
        const response = await fetch(`http://localhost:8000/link?${params}`);
        if (!response.ok) throw new Error('Failed to fetch link events');
        const events = await response.json();
        updateTable(events);
    } catch (err) {
        console.error('Error fetching link events:', err);
    }
}

async function fetchNodeEvents() {
    const selectedModes = getSelectedModes();
    if (!selectedModes.length) return;   

    const params = new URLSearchParams({ node: appState.selected_node, time: metadata.time_steps[appState.timeStep], modes: selectedModes.join(',') });

    try {
        const response = await fetch(`http://localhost:8000/node?${params}`);
        if (!response.ok) throw new Error('Failed to fetch node events');
        const events = await response.json();
        updateTable(events);
    } catch (err) {
        console.error('Error fetching link events:', err);
    }

}

function updateTable(events) {
    const tbody = document.querySelector('#rightPanel table tbody');
    tbody.innerHTML = '';

    if (!events || events.length === 0) {
        const row = document.createElement('tr');
        const cell = document.createElement('td');
        cell.colSpan = 4;
        cell.textContent = 'No events found for this link at selected time/modes.';
        row.appendChild(cell);
        tbody.appendChild(row);
        return;
    }

    events.forEach(event => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${event.from}</td>
            <td>${event.to}</td>
            <td>${event.category}</td>
            <td>${event.quantity}</td>
        `;
        tbody.appendChild(row);
    });
}


async function updateGraph() {
    updateIntensity();
    updateColorBarLabel();
    if (appState.selected_link !== null)
        fetchLinkEvents();
    if (appState.selected_node !== null)
        fetchNodeEvents();
}

fetchMetadata();
