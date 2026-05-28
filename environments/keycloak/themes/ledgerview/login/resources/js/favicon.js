(function () {
    var css = document.querySelector('link[href*="ledgerview.css"]');
    if (!css) return;
    var base = css.href.replace(/css\/ledgerview\.css.*$/, '');
    var link = document.querySelector("link[rel*='icon']") || document.createElement('link');
    link.rel = 'icon';
    link.type = 'image/svg+xml';
    link.href = base + 'img/favicon.svg';
    document.head.appendChild(link);
})();
