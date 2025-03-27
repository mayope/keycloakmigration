---
author: klg71
layout: post
title:  "Localization Migrations"
date:   2020-07-03 12:22:20 +0200
permalink: /migrations/localization/
---
# Localization Migrations
All migrations referring to the localization resource.

## addLocalizationEntry
adds a new localization entry, throws error if the entry exists for the same locale

### Parameters
- realm: String, optional
- locale: String, not optional,
- key: String, not optional,
- text: String, not optional

### Example
```yaml
    id: add-localization-entry
    author: isaac
    changes:
      - addLocalizationEntry:
          locale: en
          key: country
          text: County
```

## updateLocalizationEntry
updates a localization entry, throws error if the entry doest not exists

### Parameters
- realm: String, optional
- locale: String, not optional,
- key: String, not optional,
- text: String, not optional

### Example
```yaml
    id: update-localization-entry
    author: isaac
    changes:
      - updateLocalizationEntry:
          locale: en
          key: country
          text: Country
```

## deleteLocalizationEntry
deletes a localization entry, throws error if the entry doest not exists

### Parameters
- realm: String, optional
- locale: String, not optional,
- key: String, not optional

### Example
```yaml
    id: delete-localization-entry
    author: isaac
    changes:
      - deleteLocalizationEntry:
          locale: en
          key: country
```